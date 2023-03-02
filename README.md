# jStats

A tool for generating easy-to-read summaries of your Apache web server logs.

## Configuration

The program's configuration file can be found at `~/.config/simplestats/simplestats.yml`.

| Key                              | Description                                                                                                                                                                                                    | Default value                                                                                       |
|----------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|-----------------------------------------------------------------------------------------------------|
| `accessLogDirectory`             | The path to your Apache log directory.                                                                                                                                                                         | `/var/log/apache2`                                                                                  |
| `accessLogName`                  | The name of your access log.                                                                                                                                                                                   | `access.log`                                                                                        |
| `readRotatedLogs`                | If you use [logrotate](https://linux.die.net/man/8/logrotate) to rotate your logs, jStats can find rotated logs in the same directory as the file above.                                                       | `true`                                                                                              |
| `logFormat`                      | The format of your log. This can usually be found in `/etc/apache2/apache2.conf`. For more information, see [the documentation for mod_log_config](https://httpd.apache.org/docs/2.4/mod/mod_log_config.html). | `"%v:%p %h %l %u %t \"%r\" %s:%>s %I %O \"%{Referer}i\" \"%{User-Agent}i\" %D %k %f \"%U\" \"%q\""` |
| `outputFilePath`                 | The path to which jStats should write its output.                                                                                                                                                              | `~/jstats.html`                                                                                     |
| `inputDateFormat`                | The format of dates within the access log. See [here](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/text/SimpleDateFormat.html) for date formatting details.                               | `"dd/MMM/yyyy:HH:mm:ss Z"`                                                                          |
| `outputDateFormat`               | The format to use for dates in the output file.                                                                                                                                                                | `"yyyy-MM-dd HH:mm:ss zzz"`                                                                         |
| `whoisTool`                      | The URL of your preferred WHOIS tool. `{{address}}` will be replaced with the IP address.                                                                                                                      | `"https://iplocation.io/ip/{{address}}"`                                                            |
| `printMalformedEntries`          | If true, the program will print a message to the standard error stream if a malformed log entry is encountered.                                                                                                | `false`                                                                                             |
| `ignoreInternalLogs`             | If true, internal requests from a loopback address (i.e. `127.0.0.1` or `::1`) will be ignored.                                                                                                                | `true`                                                                                              |
| `timeTakenBuckets`               | The "Time Taken" section uses these numbers for ranges, i.e. `< 100`, `100-500`, and `&geq; 500`. This list must have at least one entry.                                                                      | `[100, 500, 1000, 5000, 10000, 50000]`                                                              |
| `truncateWideColumns`            | Truncate long strings after this many characters. Set to 0 to disable truncation.                                                                                                                              | `100`                                                                                               |
| `ipRequestCountThreshold`        | If fewer than this many requests have  been made from an IP address, it will be omitted from the output table.                                                                                                 | `5`                                                                                                 |
| `userAgentRequestCountThreshold` | If fewer than this many requests have been made from a user agent, it will be omitted from the output table.                                                                                                   | `3`                                                                                                 |
| `fileRequestCountThreshold`      | If fewer than this many requests have been made for a file, it will be omitted from the output table.                                                                                                          | `0`                                                                                                 |
| `queryRequestCountThreshold`     | If fewer than this many requests have been made with a query string, it will be omitted from the output table.                                                                                                 | `0`                                                                                                 |
| `refererRequestCountThreshold`   | If fewer than this many requests have been made from a referer, it will be omitted from the output table.                                                                                                      | `0`                                                                                                 |
| `logVerbosity`                   | The verbosity of messages generated by jStats while running. Valid settings: 0 (debug), 1 (info), 2 (warnings), 3 (errors), or 4 (silent).                                                                     | `1`                                                                                                 |
