/*
 * Copyright 2016 Vladimir Konstantinov, Yuriy Gintsyak
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.oxydev.korro.http.internal.server.actor

import org.oxydev.korro.http.api.HttpRequest
import org.oxydev.korro.http.internal.server.route.{ActorPathRoute, ActorRefRoute, RouteInfo}

import akka.actor.{Actor, ActorRef, ActorRefFactory, Props}
import io.netty.channel.Channel

class HttpRequestParentActor extends Actor {

  override def receive = {

    case HttpRequestParentActor.NewRequest(channel, route, req) =>
      val child = context.actorOf(HttpRequestActor.props(channel, route.instructions, s"${req.method} ${req.path}"))
      route.dst match {
        case ActorRefRoute(ref) => ref tell (req, child)
        case ActorPathRoute(path) => context.actorSelection(path) tell (req, child)
      }
  }
}

object HttpRequestParentActor {

  def create()(implicit factory: ActorRefFactory): ActorRef = factory.actorOf(props, "req")

  def props: Props = Props(new HttpRequestParentActor)

  case class NewRequest(channel: Channel, route: RouteInfo, req: HttpRequest)
}
