/*
 * Copyright (C) 2015, 2016  Vladimir Konstantinov, Yuriy Gintsyak
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.oxydev.korro.http.internal.common.handler

import org.oxydev.korro.util.log.Logger

import io.netty.channel.ChannelHandler.Sharable
import io.netty.channel.{ChannelDuplexHandler, ChannelHandlerContext, ChannelPromise}

import java.net.SocketAddress

/**
 * TODO: Add description.
 *
 * @author Vladimir Konstantinov
 */
@Sharable
class LoggingHandler(name: String) extends ChannelDuplexHandler {

  private val logger = Logger(name)

  private def enabled: Boolean = logger.isTraceEnabled

  private def log(ctx: ChannelHandlerContext, message: String): Unit = {
    logger.trace("{} {}", ctx.channel, message)
  }

  private def log(ctx: ChannelHandlerContext, name: String, msg: Any): Unit = {
    log(ctx, FormatUtils.formatMessage(name, msg))
  }

  override def channelRegistered(ctx: ChannelHandlerContext): Unit = {
    if (enabled) log(ctx, "REGISTERED")
    super.channelRegistered(ctx)
  }

  override def channelUnregistered(ctx: ChannelHandlerContext): Unit = {
    if (enabled) log(ctx, "UNREGISTERED")
    super.channelUnregistered(ctx)
  }

  override def channelActive(ctx: ChannelHandlerContext): Unit = {
    if (enabled) log(ctx, "ACTIVE")
    super.channelActive(ctx)
  }

  override def channelInactive(ctx: ChannelHandlerContext): Unit = {
    if (enabled) log(ctx, "INACTIVE")
    super.channelInactive(ctx)
  }

  override def exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable): Unit = {
    if (enabled) log(ctx, "EXCEPTION: " + cause)
    super.exceptionCaught(ctx, cause)
  }

  override def userEventTriggered(ctx: ChannelHandlerContext, evt: Any): Unit = {
    if (enabled) log(ctx, "USER_EVENT: " + evt)
    super.userEventTriggered(ctx, evt)
  }

  override def bind(ctx: ChannelHandlerContext, localAddress: SocketAddress, future: ChannelPromise): Unit = {
    if (enabled) log(ctx, "BIND(" + localAddress + ')')
    super.bind(ctx, localAddress, future)
  }

  override def connect(ctx: ChannelHandlerContext, remoteAddress: SocketAddress, localAddress: SocketAddress, future: ChannelPromise): Unit = {
    if (enabled) log(ctx, "CONNECT(" + remoteAddress + ", " + localAddress + ')')
    super.connect(ctx, remoteAddress, localAddress, future)
  }

  override def disconnect(ctx: ChannelHandlerContext, future: ChannelPromise): Unit = {
    if (enabled) log(ctx, "DISCONNECT()")
    super.disconnect(ctx, future)
  }

  override def close(ctx: ChannelHandlerContext, future: ChannelPromise): Unit = {
    if (enabled) log(ctx, "CLOSE()")
    super.close(ctx, future)
  }

  override def deregister(ctx: ChannelHandlerContext, future: ChannelPromise): Unit = {
    if (enabled) log(ctx, "DEREGISTER()")
    super.deregister(ctx, future)
  }

  override def channelRead(ctx: ChannelHandlerContext, msg: Any): Unit = {
    if (enabled) log(ctx, "RECEIVED", msg)
    super.channelRead(ctx, msg)
  }

  override def write(ctx: ChannelHandlerContext, msg: Any, promise: ChannelPromise): Unit = {
    if (enabled) log(ctx, "WRITE", msg)
    super.write(ctx, msg, promise)
  }

  override def flush(ctx: ChannelHandlerContext): Unit = {
    if (enabled) log(ctx, "FLUSH")
    super.flush(ctx)
  }
}

private [handler] object FormatUtils extends io.netty.handler.logging.LoggingHandler {
  override def formatMessage(eventName: String, msg: scala.Any): String = super.formatMessage(eventName, msg)
}
