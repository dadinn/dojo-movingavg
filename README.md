# dojo-movingavg

Command line tool to calculate moving average on a stream of random values.

## Usage

After cloning the repository, it can be run by calling the main
method with a simple [Leiningen](https://leiningen.org) command:

> lein run ITEMCOUNT WINDOWSIZE MAXVALUE DELAY

The command line arguments can be described as the following:

 * __ITEMCOUNT__ the number of random values to generate as input stream
 * __WINDOWSIZE__ the size of the sample window for the calculation of
   the moving average
 * __MAXVALUE__ the maximum value for the random numbers to be generated
 * __DELAY__ the delay between the generation of random values (milliseconds)

The following command therefore generates 1000 random numbers, with
maximum value of 5000 and a delay of 500ms between each number
generated, and calculates the simple moving average with a sample
size of 5:

> lein run 1000 5 5000 500

## License

Copyright © 2017 Daniel Dinnyes

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
