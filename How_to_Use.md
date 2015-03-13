# How to Use

# Introduction #

This program is simple converter from xml to excel format for bazaar log file.


# Usage #

At first, extract all patch history from bazaar repository.

D:\sample> bzr -v log --xml full\_history.xml

And then, modify the codes of main function in ReadFromXmlWriteToExcel.

ReadFromXmlWriteToExcel reader = new ReadFromXmlWriteToExcel("D:\\sample\\full\_history.xml", "D:\\sample\\full\_history.xls");

And, run it.
