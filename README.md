# SatelliteControl
Small app simulating control system for satellite swarm, build with Scala, Akka and SQLite.

## Usage
Use `sbt run` to start example scenario with 3 monitoring stations and 100 satellites. Each station twice checks status of 50 satellites, and then one station checks total number of errors for every satellite in DB.