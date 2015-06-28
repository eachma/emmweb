package org.em.web;
/**
 * eachma在netty官方例子上做了必要的改造
 * 1 增加了反射式路由
 * 2 去掉了程序生成form，需要测试者自己搭建好nginx或者把官方相关功能加上。
 * 3 享受netty的风驰电掣！
 */
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.multipart.Attribute;
import io.netty.handler.codec.http.multipart.DefaultHttpDataFactory;
import io.netty.handler.codec.http.multipart.DiskAttribute;
import io.netty.handler.codec.http.multipart.DiskFileUpload;
import io.netty.handler.codec.http.multipart.FileUpload;
import io.netty.handler.codec.http.multipart.HttpDataFactory;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder.EndOfDataDecoderException;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder.ErrorDataDecoderException;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder.IncompatibleDataDecoderException;
import io.netty.handler.codec.http.multipart.InterfaceHttpData;
import static io.netty.handler.codec.http.HttpHeaders.Values;
import io.netty.handler.codec.http.multipart.InterfaceHttpData.HttpDataType;
import io.netty.util.CharsetUtil;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static io.netty.buffer.Unpooled.*;
import static io.netty.handler.codec.http.HttpHeaders.Names.*;

public class EWebHandler extends SimpleChannelInboundHandler<HttpObject> {
	private static final Logger logger = Logger.getLogger(EWebHandler.class
			.getName());

	private HttpRequest request;

	private boolean readingChunks;

	private static final HttpDataFactory factory = new DefaultHttpDataFactory(
			DefaultHttpDataFactory.MINSIZE); 

	private HttpPostRequestDecoder decoder;
	// handler 参数
	private Map<String, List<String>> decodedQuery;
	private Map<String, List<String>> decodedFormData = new HashMap<String, List<String>>();
	private List<FileUpload> uploadFileList = new ArrayList<FileUpload>();
	// result将在response时调用，第0个是contenttype，第1个是要回传的内容
	private List<String> result = new ArrayList<String>();
	private static final String HANDLER_PREFIX = "org.em.web.handlers.";
	// handler 类和方法反射调用
	private Class<?> actionClass = null;
	private String methodName = "";
	private Method handler;
	// 处理纯粹流（字符）
	private final StringBuilder rawstr = new StringBuilder();
	// 上传使用系统默认
	static {
		DiskFileUpload.deleteOnExitTemporaryFile = true;
		DiskFileUpload.baseDirectory = null;
		DiskAttribute.deleteOnExitTemporaryFile = true;
		DiskAttribute.baseDirectory = null;
	}

	@Override
	public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
		if (decoder != null) {
			decoder.cleanFiles();
		}
	}
	// 读取和分析包、反射式路由 msg将有两种形式，封装不同的http包：REQUEST 和 HTTPCONTENT包
	@Override
	public void channelRead0(ChannelHandlerContext ctx, HttpObject msg)
			throws Exception {

		if (msg instanceof HttpRequest) {
			HttpRequest request = this.request = (HttpRequest) msg;
			QueryStringDecoder qs = new QueryStringDecoder(request.getUri());
			String path = qs.path();

			String[] sps = path.split("/");
			if (sps.length > 2) {
				methodName = sps[2];
				try {
					actionClass = Class.forName(HANDLER_PREFIX + sps[1]);

				} catch (ClassNotFoundException e) {
					e.printStackTrace();
					return;
				} catch (SecurityException e) {
					e.printStackTrace();
					return;
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
					return;
				}

			} else {
				return;

			}
			System.err.println(request.getMethod());

			if (request.getMethod().equals(HttpMethod.GET)) {
				handler = actionClass.getMethod(methodName, new Class[] {
						Map.class, List.class, String.class });
				handler.invoke(actionClass, new Object[] { qs.parameters(),
						this.result, "sessionID"});
				writeResponse(ctx.channel());

				return;
			}
			// 如果POST
			this.decodedQuery = qs.parameters();

			try {
				decoder = new HttpPostRequestDecoder(factory, request);
			} catch (ErrorDataDecoderException e1) {
				e1.printStackTrace();

				writeResponse(ctx.channel());
				ctx.channel().close();
				return;
			} catch (IncompatibleDataDecoderException e1) {

				writeResponse(ctx.channel());
				return;
			}

			readingChunks = HttpHeaders.isTransferEncodingChunked(request);

			if (readingChunks) {
				readingChunks = true;
			}

		}

		// 由于netty限制，需循环读包
		if (decoder != null) {
			if (msg instanceof HttpContent) {

				HttpContent chunk = (HttpContent) msg;
				ByteBuf content = chunk.content();
	            if (content.isReadable()) {
	                rawstr.append(content.toString(CharsetUtil.UTF_8));
	               
	            }
				try {
					decoder.offer(chunk);
				} catch (ErrorDataDecoderException e1) {
					e1.printStackTrace();

					writeResponse(ctx.channel());
					ctx.channel().close();
					return;
				}

				readHttpDataChunkByChunk();
				// 循环读包到最后一块，执行post相关的handler
				if (chunk instanceof LastHttpContent) {
					if (actionClass != null) {
						handler = actionClass.getMethod(methodName,
								new Class[] { Map.class, Map.class, List.class,
										List.class,String.class,String.class });
						handler.invoke(actionClass, new Object[] {
								this.decodedQuery, this.decodedFormData,
								this.uploadFileList, this.result ,"sessionID",rawstr.toString()});

					}
					writeResponse(ctx.channel());
					readingChunks = false;
					reset();
				}
			}
		} else {
			writeResponse(ctx.channel());
		}

	}

	/**
	 * 读包完毕后进行重置、移除产生的临时文件
	 */

	private void reset() {
		request = null;
		decoder.destroy();
		decoder = null;
	}

	/**
	 * 按块读包
	 */
	private void readHttpDataChunkByChunk() {

		try {
			while (decoder.hasNext()) {
				InterfaceHttpData data = decoder.next();
				if (data != null) {
					try {

						decodeFormData(data);
					} finally {
						data.release();
					}
				}
			}
		} catch (EndOfDataDecoderException e1) {
			

		}
	}
	/**
	 * 解包
	 * @param data
	 */

	private void decodeFormData(InterfaceHttpData data) {
		//处理form中的属性数据
		if (data.getHttpDataType() == HttpDataType.Attribute) {
			Attribute attribute = (Attribute) data;
			String value;
			try {
				value = attribute.getValue();

				List<String> ls = new ArrayList<String>();
				ls.add(value);
				this.decodedFormData.put(attribute.getName(), ls);// bug
			} catch (IOException e1) {
				// Error while reading data from File, only print name and error
				e1.printStackTrace();

				
			}
			return;

		}
		else if (data.getHttpDataType() == HttpDataType.FileUpload) {
			FileUpload fileUpload = (FileUpload) data;
			if (fileUpload.isCompleted()) {
				if (fileUpload.length() < 10000000) {
					this.uploadFileList.add(fileUpload);

				} else {

				}

			} else {

			}
			return;
		}

	}
	/**
	 * 网络通道回写
	 * @param channel
	 */

	private void writeResponse(Channel channel) {
		
		if (this.result.size() != 2) {
			this.result.clear();
			this.result.add("text/plain");
			this.result.add("error");
		}
		
		ByteBuf buf = copiedBuffer(this.result.get(1), CharsetUtil.UTF_8);
		boolean close = HttpHeaders.Values.CLOSE.equalsIgnoreCase(request
				.headers().get(CONNECTION))
				|| request.getProtocolVersion().equals(HttpVersion.HTTP_1_0)
				&& !HttpHeaders.Values.KEEP_ALIVE.equalsIgnoreCase(request
						.headers().get(CONNECTION));

		// response
		FullHttpResponse response = new DefaultFullHttpResponse(
				HttpVersion.HTTP_1_1, HttpResponseStatus.OK, buf);
		response.headers().set(CONTENT_TYPE, this.result.get(0));
		
		// websocket 需要close进行长连接
		if (!close) {			
			response.headers().set(CONTENT_LENGTH, buf.readableBytes());
			response.headers().set(CONNECTION, Values.KEEP_ALIVE);
		}

		// Write the response.
		ChannelFuture future = channel.writeAndFlush(response);
		
		if (close) {
			future.addListener(ChannelFutureListener.CLOSE);
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
			throws Exception {
		logger.log(Level.WARNING, "got wrong", cause);
		ctx.channel().close();
	}

}
