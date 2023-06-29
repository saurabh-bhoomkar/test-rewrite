receivers:
  # Specify your receivers here

processors:
  # Specify your processors here

exporters:
  # Specify your exporters here
  my_exporter:
    # Other exporter configurations...
    retry_on_failure:
      enabled: true
      initial_interval: 5s  # Initial interval between retries
      max_interval: 30s     # Maximum interval between retries
      max_elapsed_time: 5m # Maximum total elapsed time for retries
    buffer:
      # File-based buffer configuration
      file:
        path: "/path/to/buffer/directory"  # Path to the directory where buffer files will be stored
        max_size: 100 MiB                  # Maximum size of each buffer file
        max_age: 24h                       # Maximum age of each buffer file
        max_backups: 10                    # Maximum number of backup buffer files
        sync_period: 5s                    # Interval between syncing buffer files to disk

extensions:
  # Specify your extensions here

service:
  pipelines:
    metrics:
      receivers:
        # Specify the receivers to be used for the metrics pipeline
      processors:
        # Specify the processors to be used for the metrics pipeline
      exporters:
        my_exporter:
          # Use the exporter configuration defined earlier

    traces:
      receivers:
        # Specify the receivers to be used for the traces pipeline
      processors:
        # Specify the processors to be used for the traces pipeline
      exporters:
        my_exporter:
          # Use the exporter configuration defined earlier
          
