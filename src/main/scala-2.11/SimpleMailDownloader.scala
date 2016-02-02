import java.util.Properties
import javax.mail.{Folder, Store, Session}
import akka.actor.{PoisonPill, Props, ActorRef, Actor}
import akka.util.Timeout
import scala.concurrent.duration._
import com.typesafe.config.ConfigFactory

import scala.concurrent.Future
import EmailMessageProtocol._
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by rajeevprasanna on 2/1/16.
  */
object SimpleMailDownloader {

  implicit val timeout = Timeout(180 seconds)
  val config = ConfigFactory.load()

  class FetchMailActor extends Actor {
    def receive = {
      case user: User if user.providerName == "xenovus" || user.providerName == "yahoo" || user.providerName == "office365" =>
        val store = getNewStoreConnection(user)
        println(s"got store connection for user => ${user.emailId}")
        store.onSuccess { case store => val actorRef = context.actorOf(Props[MailDownloaderTriggerActor])
          actorRef ! StoreClass(store, user)
        }
        store.onFailure { case failure => println(s"Mail downloader failed for user => ${user.emailId} with error => ${failure}") }
      case _ => println("sucking email type")
    }
  }

  class MailDownloaderTriggerActor extends Actor {
    var foldersList: List[String] = List.empty
    var store: Store = _

    def receive = {
      case StoreClass(s, user) =>
        store = s
        val actorRef = context.actorOf(Props[ExtractMessagesCounterActor])
        foldersList = AppConfig.stringValues(user.providerName + ".folders")
        val folder = store.getFolder(foldersList.head)
        foldersList = foldersList.tail
        folder.open(Folder.READ_ONLY)
        actorRef ! InitMailDownload(user, folder)

      case ExtractFolderMails(user) if !foldersList.isEmpty =>
        val actorRef = context.actorOf(Props[ExtractMessagesCounterActor])
        val folder = store.getFolder(foldersList.head)
        foldersList = foldersList.tail
        folder.open(Folder.READ_ONLY)
        actorRef ! InitMailDownload(user, folder)
    }
  }

  class ExtractMessagesCounterActor extends Actor {
    var downloadableBatcheIndex = 0
    var batches: List[(Int, Int)] = List.empty
    var parentMailDownloaderActor: ActorRef = _

    def receive = {
      case InitMailDownload(user, folder) => batches = getBatchesRange(user, folder.getMessageCount)
        parentMailDownloaderActor = sender
        val actorRef = context.actorOf(Props[BatchMsgDownloader])
        if (batches.size > 0) {
          val x = batches(downloadableBatcheIndex)
          actorRef ! MailDownloadBatch(folder, x._1, x._2, user)
        }


      case BatchDownloadCompleted(folder, user) => downloadableBatcheIndex += 1
        if (batches.size > 0 && downloadableBatcheIndex == batches.size-1) {
          println(s"Mail download completed for user => ${user.emailId} with folder => ${folder.getName}")
          folder.close(true)
          parentMailDownloaderActor ! ExtractFolderMails(user)
        } else if(downloadableBatcheIndex < batches.size){
          val actorRef = context.actorOf(Props[BatchMsgDownloader])
          val x = batches(downloadableBatcheIndex)
          actorRef ! MailDownloadBatch(folder, x._1, x._2, user)
        }
    }
  }

  class BatchMsgDownloader extends Actor {
    def receive = {
      case MailDownloadBatch(folder, start, end, user) =>
        println(s"user => ${user.emailId}  => Downloading batch range from ${start} to ${end} with folder name ${folder.getName}")
        val rawMessages = folder.getMessages(start, end)
        for (m <- rawMessages) {
          println(s"user => ${user.emailId}  => Downloading message with subject => ${m.getSubject}  and date => ${m.getReceivedDate}")
        }
        sender ! BatchDownloadCompleted(folder, user)
    }
  }

  def getNewStoreConnection(user: User): Future[Store] = {
    Future {
      val props = getPropsOfProvider(user.providerName)
      val session = Session.getInstance(props)
      val store = session.getStore("imaps")
      store.connect(props.getProperty("mail.imap.host"), user.emailId, user.password)
      store
    }
  }

  def getPropsOfProvider(providerName: String): Properties = {
    val props = new Properties()
    val host = config.getString(providerName + ".host")
    val port = config.getString(providerName + ".port")

    props.put("mail.imap.host", host)
    props.put("mail.imap.port", port)
    props.setProperty("mail.store.protocol", "imaps")
    props
  }

  def getBatchesRange(user: User, totalMsgCount: Int): List[(Int, Int)] = {
    val start = config.getInt("MainDownloader.start") //replace if you have downloaded any mails already
    val batchSize = config.getInt("MainDownloader.batchSize")
    (start to totalMsgCount).sliding(batchSize, batchSize).map(x => (x.head, x.last)).toList
  }
}