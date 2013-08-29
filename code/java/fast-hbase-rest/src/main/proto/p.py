#!/usr/bin/env python
#coding:utf-8
#Copyright (C) dirlt

import time
import httplib
import sys
import socket
import message_pb2

def construct():
    request = message_pb2.ReadRequest()
    
    request.table_name='appbenchmark'
    request.row_key='2012-04-08_YULE'
    request.column_family='stat'
    request.qualifiers.append('14_day_active_count_avg')

    data = request.SerializeToString()
    return data

def query(conn,data):
    try:
        conn.request('POST','/read',data)
        data2 = conn.getresponse().read()
        return True
    except Exception,e:
        conn.close()
        return False

def makeConnection(host,port):
    s = time.time()
    conn = httplib.HTTPConnection(host,port)
    e = time.time()
    return (conn,e-s)
    
def func(host,port,timeout):
    data = construct()
    (conn,_) = makeConnection(host,port)
    succeed = 0
    all = 0
    begin = time.time()
    cTime = 0
    while True:
        all+=1
        if query(conn,data):
            succeed+=1
        else:
            (conn,t) = makeConnection(host,port)
            cTime += t
        if all == 1000:
            end = time.time()
            print 'ratio: %4d/%4d = %7.3f%%, avg time: %6.2fms, avg conn time: %6.2fms'%(succeed, all, succeed * 100.0 / all,
                                                                                         (end - begin - cTime) * 1000.0 / all,
                                                                                         cTime * 1000.0 / (all - succeed + 0.01))            
            
            sys.stdout.flush()
            begin = end
            all = 0
            succeed = 0
            cTime = 0

def main(host,port,timeout):
    socket.setdefaulttimeout(timeout * 0.001)
    func(host,port,timeout)
        
def Main(args):
    for arg in args:
        if arg.startswith('--host='):
            host = arg[len('--host='):]
        elif arg.startswith('--port='):
            port = int(arg[len('--port='):])
        elif arg.startswith('--timeout='):
            timeout = int(arg[len('--timeout='):])
    main(host,port,timeout)

if __name__ == '__main__':
    Main(sys.argv)
        