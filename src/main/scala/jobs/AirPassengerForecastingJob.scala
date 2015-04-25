import com.twitter.scalding._

import com.prognos.Series
import com.prognos.forecast.HoltLinear

class AirPassengerForecastingJob(args:Args) extends Job(args:Args){
  val schema = List('year, 'country, 'passengers)

  Csv(args("input"), ",", schema).
    mapTo(('year, 'country, 'passengers) -> ('year, 'country, 'passengers)) {
      x:(String, String, String) =>
        val (year, country, passengers) = x
        (year.toInt, country, passengers.toDouble)
    }.groupBy('country) { group =>
        group.sortBy('year).
          mapList[Double,Double]('passengers -> 'passengers){observations:List[Double] =>
            forecast(observations)
          }
    }.write(Csv(args("output")))

  def forecast(observations:List[Double]):Double = {
    val series = new Series(observations.toArray)
    val algo = new HoltLinear
    val (alpha, beta, algoType, horizon) = (0.8, 0.2, "simple", 1)
    val forecasts = algo.calculate(series, alpha, beta, algoType, horizon)
    forecasts(0)
  }
}
