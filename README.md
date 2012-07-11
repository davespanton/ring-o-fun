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


License
-------

Copyright (C) 2012 Dave Spanton

Distributed under the Eclipse Public License, the same as Clojure.