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

URL = 'http://localhost:12347/tell'

def testPOST():
    json = {"reqid":"3",
            "account":'dirlt',
            'timeout':1000,
            'reqtype':'geographic',
            'imei':'123'
            }
    data = raiseHTTPRequest(URL,jsonToString(json))
    print data

def testMultiPOST():
    json = {"reqid":"3",
            "account":'dirlt',
            'timeout':1000,
            'reqtype':'geographic',
            'imei':'123'
            }
    array = [json,json,json]
    data = raiseHTTPRequest(URL,jsonToString(array))
    print data
    

def testGET():
    url = URL + '?reqid=3&account=dirlt&timeout=1000&reqtype=geographic&imei=123'    
    data = raiseHTTPRequest(url)
    print data
    

if __name__ == '__main__':
    testPOST()
    testMultiPOST()
    testGET()

    
            
            
