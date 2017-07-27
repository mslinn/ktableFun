import java.lang
import java.util.Properties
import java.util.concurrent.TimeUnit.SECONDS
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.kstream.{KStream, KStreamBuilder, KTable}
import org.apache.kafka.streams.{KafkaStreams, StreamsConfig}
import scala.collection.JavaConverters._

/** Slightly improved version of [[https://kafka.apache.org/documentation/streams/ the official docs]]
  *
  * * HOW TO RUN THIS EXAMPLE
  *
  * 1) Start Zookeeper and Kafka.
  * See the <a href='http://docs.confluent.io/current/quickstart.html#quickstart'>QuickStart</a>.
  * {{{
  * $ cd /opt/kafka_2.12-0.11.0.0/
  * $ bin/zookeeper-server-start.sh config/zookeeper.properties &
  * $ bin/kafka-server-start.sh config/server.properties &
  * }}}
  *
  * 2) Create the input and output topics used by this example.
  * {{{
  * $ bin/kafka-topics.sh --create --topic TextLinesTopic --zookeeper localhost:2181 --partitions 3 --replication-factor 1
  * $ bin/kafka-topics.sh --create --topic WordsWithCountsTopic --zookeeper localhost:2181 --partitions 3 --replication-factor 1
  * }}}
  *
  * 3) Package the app
  * {{{ sbt assembly }}}
  *
  * 4) Start the first instance of the application on port 7070:
  * {{{
  * $ java -cp target/scala-2.12/ktableFun-assembly-0.1.0.jar WordCountApplication
  * }}}
  *
  * Here, `7070` sets the port for the REST endpoint that will be used by this application instance.
  *
  * Then, in a separate terminal, run the second instance of this application (on port 7071):
  * {{{
  * $ java -cp target/streams-examples-3.2.2-standalone.jar \
  *      io.confluent.examples.streams.interactivequeries.InteractiveQueriesExample 7071
  * }}}
  *
  * 4) Write some input data to the source topics (e.g. via {@link WordCountInteractiveQueriesDriver}). The
  * already running example application (step 3) will automatically process this input data
  *
  * 5) Use your browser to hit the REST endpoint of the app instance you started in step 3 to query
  * the state managed by this application.  Note: If you are running multiple app instances, you can
  * query them arbitrarily -- if an app instance cannot satisfy a query itself, it will fetch the
  * results from the other instances.
  *
  * For example,
  *  - List all running instances of this application: http://localhost:7070/state/instances
  *  - List app instances that currently manage (parts of) state store "word-count" http://localhost:7070/state/instances/word-count
  *  -  Get all key-value records from the "word-count" state store hosted on a the instance running localhost:7070
  * http://localhost:7070/state/keyvalues/word-count/all
  *  - Find the app instance that contains key "hello" (if it exists) for the state store "word-count"
  * http://localhost:7070/state/instance/word-count/hello
  *  - Get the latest value for key "hello" in state store "word-count"
  * http://localhost:7070/state/keyvalue/word-count/hello
  * }}}
  *
  * Note: that the REST functionality is NOT part of Kafka Streams or its API. For demonstration
  * purposes of this example application, we decided to go with a simple, custom-built REST API that
  * uses the Interactive Queries API of Kafka Streams behind the scenes to expose the state stores of
  * this application via REST.
  *
  * 6) Once you're done with your experiments, you can stop this example via `Ctrl-C`.  If needed,
  * also stop the Kafka broker (`Ctrl-C`), and only then stop the ZooKeeper instance (`Ctrl-C`).
  *
  * If you like you can run multiple instances of this example by passing in a different port. You
  * can then experiment with seeing how keys map to different instances etc. */
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
