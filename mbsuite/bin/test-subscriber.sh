
 #!/bin/bash

java -d64 -Xms4048M -Xmx14048M  -cp  "../ext-lib/*":"../ext-lib/rti/*":"../export/data-files/":"../lib/mbsuite-addons.jar":"../config/":"../lib/mbsuite-subscriber-base.jar":"../export/"    com.persistent.bcsuite.process.Extractor $1 $2 $3 $4

 