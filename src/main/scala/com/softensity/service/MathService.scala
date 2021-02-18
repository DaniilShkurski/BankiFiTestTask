package com.softensity.service

class MathService {

  def functionalPrimeNumbers(number: Integer): List[Int] = {
    (1 to number).filter(isPrime).toList
  }

  def streamingPrimeNumbers(number: Integer): List[Int] = {
    (2 #:: LazyList.from(3,2).filter(isPrime).takeWhile(_ < number)).toList
  }

  private def isPrime(number: Int): Boolean = {
    if (number <= 1) false
    else if (number == 2) true
    else !(2 until number).exists(x => number % x == 0)
  }

}
