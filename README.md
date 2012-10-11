Ring'o'fun
==========

Sits between client and CCO, making little changes to what goes by.

Usage
-----

Launch some flavour of repl.

Get setup.

	(use 'ring-o-fun.core)
	(use 'ring.util.serve)
	(serve handler 3000) ;; or whatever port

Point the client to the local server.

At this point requests will just flow through.

	(grab!)

to pick up the whole of the last category. Now every request will be served with the contents of the grabbed atom...

Which you can manipulate. There are some convenience methods in ring-o-fun.xml to help with this.

	(release!)

to return to passing data through.

Once you've grabbed some data. Here's a few things to do:

	(require ['ring-o-fun.xml :as 'rxml])
	;; returns a count of atom:entry items currently grabbed
	(rxml/count-entries @grabbed)
	;; returns a new map with the first 5 atom:entry items removed and also updates os:totalResults to match
	(rxml/ammend-entries rxml/drop-entries 5 @grabbed) 

	(require ['ring-o-fun.zip :as 'rzip])
	;; return a new zipper with the yv:serviceId of the first atom:entry changed to 1193
	(rzip/feed=> [(rzip/tag :atom:entry) (rzip/tag :yv:serviceId) (rzip/edit (rzip/content) "1193")] @grabbed)

License
-------

Copyright (C) 2012 Dave Spanton

Distributed under the Eclipse Public License, the same as Clojure.
