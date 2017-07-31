import java.lang
import java.util.Properties
import java.util.concurrent.TimeUnit.SECONDS
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.kstream.{KStream, KStreamBuilder, KTable}
import org.apache.kafka.streams.{KafkaStreams, StreamsConfig}
import scala.collection.JavaConverters._

/** Slightly improved version of [[https://kafka.apache.org/documentation/streams/ the official docs]]
  * Use `bin/run` to run this example.
  * Once you're done, stop this example via `Ctrl-C`. */
object WordCountApplication extends App {
  val config = {
    val p = new Properties
    p.put(StreamsConfig.APPLICATION_ID_CONFIG, "wordcount-application")
    p.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092")
    p.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String.getClass)
    p.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String.getClass)
    p
  }

  val builder: KStreamBuilder = new KStreamBuilder
  val textLines: KStream[String, String] = builder.stream("TextLinesTopic")
  val wordCounts: KTable[String, lang.Long] =
    textLines
      .flatMapValues { _.toLowerCase.split("\\W+").toIterable.asJava }
      .groupBy((_, word) => word)
      .count("Counts")
  wordCounts.to(Serdes.String, Serdes.Long, "WordsWithCountsTopic")
  val streams = new KafkaStreams(builder, config)
  streams.start()
  Runtime.getRuntime.addShutdownHook(new Thread {
    streams.close(10, SECONDS)
  })
}
