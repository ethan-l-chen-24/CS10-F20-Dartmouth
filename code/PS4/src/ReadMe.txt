Kevin Bacon Game
 @name -> Ethan Chen
 @date -> October 29, 2020
 @class -> CS 10, Fall 2020, Pierson

The boundary cases/files I have tested are

    emptyTest.txt - tests to make sure that the program can throw an error if it tries to read an empty txt file - works
        - The associated picture is emptyBoundary

    repeated___.txt - tests to make sure that if names of movies are repeated in their files, or even the movie-actors
                      builds a link more than once, that the program can eliminate these repeats and create a graph
                      successfully without duplicates - works.
        - The associated picture is repeatsBoundary

    wrongFormatTest.txt - tests to make sure that if the file is of the incorrect format ("integer | name of movie") it
                          will throw an error - works.
        - The associated picture is incorrectFormatBoundary

The other pictures are:

    Test - some output when playing the game with the test list
    AllActors - some output when playing the game with the full list
    FindKevinBacons - the output when running the "find other kevin bacons" method
    ActorsGraph - the graph outputted when using the full list (kinda hard to tell what's going on because so many names
    TestMapsAndGraph - the maps and the graph outputted when using the test list - more clear

Based on my program, I believe that Gene Hackman or Robert De Niro are the best candidates for being Kevin Bacons.
They both scored extremely well, both having shared movies with numerous other actors and are in very close contact
to mostly everyone

