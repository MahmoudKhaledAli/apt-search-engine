# apt-search-engine
APT Project</br>
To run the project:</br>
Compile all the java files, make sure that you have Jsoup and Java DB Driver libraries in your classpath.</br>
Also make sure that you have an Apache DB Server running on localhost:1527</br>
Run the Crawler class, it will download the html documents in a docs folder and populate the database. (To reset the crawler, delete toVisit.txt and visited.txt and run it again.)</br>
Run the Indexer class, it will index the pages downloaded in the docs folder and populate the database.</br>
Deploy the servlet classes and the web.xml to your Tomcat Apache server, and navigate to the app.</br>
