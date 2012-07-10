import time
def test_handle(request):
    aa = request.read()
    request.write("time is now "+str(request)+" "+aa)

def failing_handle(request):
    raise Exception('spamm', 'egs')
