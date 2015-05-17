# CardSwipeListener
This repo contains code if you want to integrate Card Swipe Views like Feedly.

As of now the swipe view listener can only be used with View Groups and its subclasses. Just instantiate the listener with the view group as constructor params and then set the view listener object as the the view group's on touch listener. You are good to go with the animation integrated and ready to run.

## Code Sample
```
RelativeLayout viewGroup = (RelativeLayout) view.findViewById(R.id.fragment);
CardSwipeListener touchListener = new CardSwipeListener(viewGroup);
viewGroup.setOnTouchListener(touchListener);
```

##License
The MIT License (MIT)

Copyright (c) 2015 Nitish Mehrotra

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.
