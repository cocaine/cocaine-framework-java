"""
    Copyright (c) 2012 Vladimir Shakhov <bogdad@gmail.com>
    Copyright (c) 2012 Other contributors as noted in the AUTHORS file.

    This file is part of Cocaine.

    Cocaine is free software; you can redistribute it and/or modify
    it under the terms of the GNU Lesser General Public License as published by
    the Free Software Foundation; either version 3 of the License, or
    (at your option) any later version.

    Cocaine is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
    GNU Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public License
    along with this program. If not, see <http://www.gnu.org/licenses/>. 
"""

import time
def test_handle(request):
    aa = request.read()
    request.write("python replies: "+str(request)+" "+aa)

def sleeping_handle(request):
    aa = request.read()
    request.write("about to sleep")
    time.sleep(3600)
    request.write("replies!!")

def failing_handle(request):
    request.write("reply")
    time.sleep(0.01)
    raise Exception('spamm', 'eпgs')

def test_handle_chunked(request):
    aa = request.read()
    request.write("first chunk "+aa)
    time.sleep(0.01)
    request.write("second_chunk")
    time.sleep(0.01)
    request.write("third_chunk")
    for i in range(10):
        request.write(str(i)+" chunk")
        time.sleep(0.01)
