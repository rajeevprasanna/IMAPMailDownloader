package idleExecutors

import java.util.Properties
import javax.mail.event.{MessageCountEvent, MessageCountAdapter}
import javax.mail.{Folder, Session, Store}

import akka.actor.{Actor, ActorSystem, Props}
import akka.util.Timeout
import com.sun.mail.imap.IMAPFolder
import scala.concurrent.duration._
import akka.util.Timeout
import com.typesafe.config.ConfigFactory

import scala.concurrent.Future
import javax.mail._
import scala.concurrent.ExecutionContext.Implicits.global


object MailSocketListener extends App {

  val mailAccounts = List(
    ("rajesdeasv@bnuqws.com", "sdbfsd1271", "secure.emailsrvr.com", "993", "INBOX")
  )

  case class User(emailId: String, password: String, hostName: String, port:String, folderName:String)
  val usersList:List[User] =  mailAccounts.map{x => User(x._1, x._2, x._3, x._4, x._5)}

  implicit val system = ActorSystem("idleListener")
  implicit val timeout = Timeout(180 seconds)

  usersList.foreach { user =>
    (1 to 10).foreach{_ => system.actorOf(Props(new IdleListener(user)))}
  }

  class IdleListener(user: User) extends Actor {

    var initlizedFolder = false
    var inboxFolder: IMAPFolder = null

    val storeF = getNewStoreConnection(user)
    storeF.onSuccess {
      case store =>
        println("Got store instance")
        val folder: Folder = store.getFolder(user.folderName)
        folder.open(Folder.READ_ONLY)
        folder.addMessageCountListener(new MessageCountAdapter {
          override def messagesAdded(ev: MessageCountEvent): Unit = {
            val msgs: Array[Message] = ev.getMessages
            println(s"Got  $msgs.length  new messages to : ${user.emailId}")
            msgs.map(m => m.getMessageNumber)
          }
        })
        val f = folder match {
          case x: IMAPFolder => println("folder instance created")
            inboxFolder = x
            initlizedFolder = true
          case _ => println("U got fucked up")
            None
        }
    }

    storeF.onFailure {
      case _ => println("fucked in failure")
    }

    context.system.scheduler.schedule(0 millisecond, 1 second, self, "idle")

    def receive = {
      case "idle" => println("running idle command")
        if (initlizedFolder) {
          inboxFolder.idle()
        }
    }
  }

  def getNewStoreConnection(user: User): Future[Store] = {
    Future {
      val props = getPropsOfProvider(user)
      val session = Session.getInstance(props)
      val store = session.getStore("imaps")
      store.connect(props.getProperty("mail.imap.host"), user.emailId, user.password)
      store
    }
  }

  def getPropsOfProvider(user: User): Properties = {
    val props = new Properties()

    props.put("mail.imap.host", user.hostName)
    props.put("mail.imap.port", user.port)
    props.setProperty("mail.store.protocol", "imaps")
    props
  }

  Thread.sleep(100000)


}