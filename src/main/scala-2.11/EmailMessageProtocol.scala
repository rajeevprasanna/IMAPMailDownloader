import javax.mail.{Folder, Store}

/**
  * Created by rajeevprasanna on 2/2/16.
  */
object EmailMessageProtocol {

  case class User(emailId:String, password:String, providerName:String)
  case class TotalFolderMailCount(count:Int, folderName: String)

  case class DownloadedInboxMails(count:Int)
  case class DownloadedSentboxMails(count:Int)

  case class StoreClass(store:Store, user:User)
  case class InitMailDownload(user:User, folder:Folder)
  case class MailAction(folder:Folder, action:String, folderName:String)

  case class MailDownloadBatch(folder:Folder, start:Int, end:Int, user:User)
  case class BatchDownloadCompleted(folder:Folder, user:User)

  case class ExtractFolderMails(user:User)
}
