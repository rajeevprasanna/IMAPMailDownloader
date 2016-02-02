import javax.security.auth.login.Configuration

import akka.actor.{Props, ActorSystem, Actor}
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.{Success, Failure, Random}
import akka.pattern.ask

import SimpleMailDownloader.FetchMailActor
import EmailMessageProtocol._
import scala.collection.JavaConverters._

/**
  * Created by rajeevprasanna on 2/1/16.
  */
object Main extends App {

  implicit val system = ActorSystem("mailDownloaderActorSystem")
  import system.dispatcher
  implicit val timeout = Timeout(20000 seconds)

  val userAccounts = List(
      List("xxx@office365.com", "password@office", "office365"),
      List("xxxxx@rackspacemail.com", "password@3423", "rackspace"),
      List("xxxxx.xxxxx@yahoo.com", "password@utsd", "yahoo")
  )

  userAccounts.map { p =>
    val x:List[String] = p
    val user = User(x(0), x(1), x(2))
    val mailDownloadActor = system.actorOf(Props[MailDownloaderActor])
    mailDownloadActor ! user
  }
}

class MailDownloaderActor extends Actor {
  def receive = {
    case user: User => val fetchMailActorRef = context.actorOf(Props[FetchMailActor], user.emailId)
      fetchMailActorRef ! user
    case _ => println("Invalid user object")
  }
}
