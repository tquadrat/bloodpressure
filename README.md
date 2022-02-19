# Introduction

I wrote this program to get a statistical overview over my blood pressure values that I can present to my doctor.

I use a website name [blutdruckdaten.de](https://blutdruckdaten.de) to collect this data, but I do not like the reports that they provide. Same for my doctor.

So I decided to write something myself that uses this data, but provides better or at least other reports.

As of now (2022-02-19), the program is by far not ready, but it allows the import of data from "blutdruckdaten.de" (via CSV) and we can generate a simple text-based report.

# Future Enhancements

I am thinking about the integration of some graphs, created with XChart, the generation of a PDF document for the report, and, of course, about a GUI for the program that would also allow entering the measuring data directly.

Another idea is to maintain the data for more that on person in the database.

Please have a look to the tasklist, too. You find it in the JavaDoc documentation.

# Limitations

Currently, the [provided Tar file](https://tquadrat.githup.io/bloodpressure/build/distributions/org.tquadrat.bloodpressure-0.0.1-amd64.tar) works for x64 Linux only. The provided Zip file is intended for Windows, but it does not work on that platform.

That is because the GUI will be built on base of JavaFX, and this relies on a bunch of platform specific libraries. Although there is no GUI yet, the libraries are already linked.

I need to create builds for all platforms (Linux x64, MacOS and Windows), but this is currently also work in progress.

# Documentation

- [Javadoc Reference](https://tquadrat.github.io/bloodpressure/javadoc/index.html)