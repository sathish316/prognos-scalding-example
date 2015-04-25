#### Prognos - Scalding usage example

This is an example of using [Prognos](https://bitbucket.org/sathish316/prognos) forecasting library in [Scalding](https://github.com/twitter/scalding#scalding) Mapreduce jobs.
Scalding template is forked from [activator-scalding](https://github.com/deanwampler/activator-scalding)

For executing job in local mode:

```
Execute Run with args:
AirPassengerForecastingJob --local --input /path/to/airpassengers.csv --output /path/to/output.csv
```

For submitting job to hadoop:

```
$ sbt assembly
$ hadoop jar /path/to/prognos-scalding-example.jar AirPassengerForecastingJob --hdfs --input hdfs://pathto/input.csv --output hdfs://pathto/output.csv
```