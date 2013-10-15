#include <cctype>
#include <string>
#include <climits>
#include <cassert>
#include <cstdio>
using namespace std;

class Solution {
public:
  typedef long long LL;
  int atoi(const char* str) {
    // Note: The Solution object is instantiated only once and is reused by each test case.
    string s = trim(str);
    return atol(s);
  }
  LL atol(const string& s) {
    bool minus = false;
    if(s.empty()) {
      return 0;
    }
    int f = 0;
    if(s[0] == '-') {
      minus = true;
      f = 1;
    } else if(s[0] == '+') {
      f = 1;
    }
    LL res = 0;
    for(int i = f; i < s.size() && isdigit(s[i]); i++) {
      res = res * 10 + (s[i] - '0');
      LL t = res;
      if(minus) {
        t = -t;
      }
      if(t >= INT_MAX || t <= INT_MIN) {
        break;
      }
    }
    if(minus) {
      res = -res;
    }
    if(res >= INT_MAX) {
      return INT_MAX;
    } else if(res <= INT_MIN) {
      return INT_MIN;
    } else {
      return res;
    }
  }
  string trim(const char* s) {
    int f = 0;
    int t = strlen(s) - 1;
    while(f <= t) {
      if(s[f] == ' ') {
        f++;
      } else {
        break;
      }
    }
    while(f <= t) {
      if(s[t] == ' ') {
        t--;
      } else {
        break;
      }
    }
    string x;
    for(int i = f; i <= t; i++) {
      x += s[i];
    }
    return x;
  }
};

int main() {
  Solution s;
  assert(s.atoi("12345") == 12345);
  return 0;
}
