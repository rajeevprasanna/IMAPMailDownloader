import com.typesafe.config.ConfigFactory

/**
  * Created by rajeevprasanna on 2/2/16.
  */
object AppConfig {
  import collection.JavaConversions._
  val config = ConfigFactory.load()

  def getListValue(key:String) = {
    config.getAnyRefList(key).toList
  }

  def stringValues(key:String):List[String] = {
    config.getStringList(key).toList
  }
}
