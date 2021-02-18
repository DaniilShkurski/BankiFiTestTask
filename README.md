# Endpoints

# `/primes/{number}?function={function}`

    number - numeric parameter
    function - string parameter with such cases:
        - "streaming" - counting prime numbers in stream
        - other cases - Bad Request
        - when parameter is not defined, non-streaming prime numbers search is used
    
# `/search?q={query}`

    query - string parameter, specifies query request
    
For using Google search, there were a couple of possible approaches:
1) parsing html response from sent request to "google.com/search" and converting information to json (which was choosed to implement)
2) building custom Json API over Google Search JSON API, which required API Key to be used in project, but that way is not very suitable in terms of security

# TODO
- unit tests (maybe integration tests too)
- logging
- extracting all configurations into config files
- configurations of akka/akka-http with usage of application.conf file
- creating OpenAPI documentation
- extract google search response index into query parameter
- number borders validation for prime numbers can be replaced by timeout (based on requirements)
