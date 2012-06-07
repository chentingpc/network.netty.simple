/*
* chentingpc @ 2012.5.5
*/

This is a very simple and centralized P2P server/client demo, and out of rush, it's STATIC for now!

How it works:

1.Server goes online (console), and server keep the seed file which the name is file name, its content is one line "host:port"

2.Client1 and Client2 both go online (console) with their own server port (which should be consistency with the host and port in seed file in server side).

3.in Client1 or Client2, input the file name in the console, if the file both exsit in server seed and in conressponding client runtime folder, then it could be downloaded.