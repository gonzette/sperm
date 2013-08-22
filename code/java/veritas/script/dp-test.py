#!/usr/bin/env python
#coding:utf-8
#Copyright (C) dirlt

import socket
import urllib2
def raiseHTTPRequest(url,data=None,timeout=3):
    # if we do post, we have to provide data.
    f=urllib2.urlopen(url,data,timeout)
    return f.read()

import json
def jsonToString(dict):
    return json.dumps(dict)
def jsonFromString(s):
    return json.loads(s)

URL = 'http://dp5:12347/tell'
def test1():
    json = {"reqid":"3",
            "account":'dirlt',
            'timeout':500,
            'reqtype':'',
            'umid':'0006652a188a867cccff4a35a4967'
            }
    data = raiseHTTPRequest(URL,jsonToString(json))
    print data

def test2():
    url = URL + '?reqid=3&account=dirlt&timeout=500&reqtype=&umid=0006652a188a867cccff4a35a4967'    
    data = raiseHTTPRequest(url)
    print data
    

if __name__ == '__main__':
    test1()
    test2()
    
