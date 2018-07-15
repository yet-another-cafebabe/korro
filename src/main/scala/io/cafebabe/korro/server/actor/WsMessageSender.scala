/*
 * Copyright (C) 2015  Vladimir Konstantinov, Yuriy Gintsyak
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
package io.cafebabe.korro.server.actor

import io.cafebabe.korro.api.ws._
import io.cafebabe.korro.internal.ChannelFutureExt

import akka.actor._
import io.netty.channel.{ChannelFuture, ChannelHandlerContext}

import scala.concurrent.duration._

object WsMessageSender {

  def create(ctx: ChannelHandlerContext)(implicit factory: ActorRefFactory): ActorRef = {
    factory.actorOf(Props(new WsMessageSender(ctx)))
  }

  case class Inbound[T <: WsMessage](msg: T)
}

class WsMessageSender(ctx: ChannelHandlerContext) extends Actor with Stash with ActorLogging {

  import context.dispatcher

  import WsMessageSender.Inbound

  private var timeoutTask = context.system.scheduler.scheduleOnce(5 seconds, self, ReceiveTimeout)

  override def receive: Receive = {

    case ReceiveTimeout =>
      log.warning("Command SetRecipient was not received in 5 seconds. Closing connection...")
      context.stop(self)

    case DisconnectWsMessage => context.stop(self)

    case Inbound(_) => stash()

    case SetRecipient(ref) =>
      schedulePingTimeout()
      unstashAll()
      context become initialized(ref)
  }

  private def initialized(ref: ActorRef): Receive = {

    case Inbound(DisconnectWsMessage) => self ! PoisonPill

    case DisconnectWsMessage => self ! PoisonPill

    case Inbound(msg) =>
      ref ! msg
      schedulePingTimeout()

    case msg: WsMessage =>
      send(msg)
      schedulePingTimeout()
  }

  private def schedulePingTimeout(): Unit = {
    timeoutTask.cancel()
    timeoutTask = context.system.scheduler.scheduleOnce(30 seconds, self, PingWsMessage)
  }

  override def postStop(): Unit = {
    timeoutTask.cancel()
    send(DisconnectWsMessage).closeChannel()
    super.postStop()
  }

  private def send(msg: Any): ChannelFuture = ctx.writeAndFlush(msg)
}
