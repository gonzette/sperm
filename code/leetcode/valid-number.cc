#include <sstream>
#include <cstring>
#include <cctype>
#include <string>
#include <algorithm>
#include <cassert>
using namespace std;

class Solution {
public:
  bool isNumber(const char* s) {
    // Note: The Solution object is instantiated only once and is reused by each test case.

    string x = trim(s);
    if(x.size() == 0) {
      return false;
    }
    size_t sep = x.find_first_of("e");
    if(sep == string::npos) {
      return isFloat(x);
    } else {
      string s1 = x.substr(0, sep);
      string s2 = x.substr(sep + 1);
      bool b1 = isFloat(s1);
      bool b2 = isInt(s2);
      return b1 && b2;
    }
  }
  bool isInt(const string& s) {
    return withSignNumber(s, false);
  }
  bool isFloat(const string& s) {
    size_t sep = s.find_first_of(".");
    if(sep == string::npos) {
      return isInt(s);
    }
    const string& x = s;
    string s1 = x.substr(0, sep);
    string s2 = x.substr(sep + 1);
    static const char* blk[] = {
      ".", ".-", ".+",
      "-.", "-.-", "-.+",
      "+.", "+.-", "+.",
      NULL
    };
    for(int i = 0; blk[i]; i++) {
      if(x == blk[i]) {
        return false;
      }
    }
    bool b1 = withSignNumber(s1, true);
    bool b2 = withoutSignNumber(s2, true);
    return b1 && b2;
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
  bool withSignNumber(const string& s, bool empty) {
    if(s.size() == 0) {
      return empty;
    }
    int f = 0;
    if(s[0] == '-' || s[0] == '+') {
      f = 1;
    }
    if(f == s.size()) {
      return empty;
    }
    for(int i = f; i < s.size(); i++) {
      if(!isdigit(s[i])) {
        return false;
      }
    }
    return true;
  }

  bool withoutSignNumber(const string& s, bool empty) {
    if(s.size() == 0) {
      return empty;
    }
    for(int i = 0; i < s.size(); i++) {
      if(!isdigit(s[i])) {
        return false;
      }
    }
    return true;
  }
};

int main() {
  Solution s;
  assert(s.isNumber(" 1 "));
  assert(!s.isNumber("1 4"));
  assert(s.isNumber("2e10"));
  assert(s.isNumber("0.1"));
  assert(!s.isNumber("1a"));
  assert(s.isNumber("-1."));
  assert(!s.isNumber("-1.-1"));
  assert(s.isNumber("-1e1"));
  assert(s.isNumber("-1e-1"));
  assert(s.isNumber("3."));
  assert(!s.isNumber("te1"));
  assert(s.isNumber("+.8"));
  assert(!s.isNumber("."));
  assert(!s.isNumber("-."));
  assert(s.isNumber("46.e3"));
  return 0;
}
