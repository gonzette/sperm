#include <string>
#include <cassert>
using namespace std;

class Solution {
public:
  bool isMatch(const char* s, const char* p) {
    // Note: The Solution object is instantiated only once and is reused by each test case.
    string xs(s);
    string xp(p);
    xp = simplify(xp);
    bool res = isMatch(xs, xp);
    return res;
  }

  string simplify(const string& p) {
    char last = '\0';
    string x;
    for(int i = 0; i < p.size(); i++) {
      if(last == '*' && p[i] == '*') {
        continue;
      } else {
        x += p[i];
        last = p[i];
      }
    }
    return x;
  }

  bool baseMatch(const string& s, const string& p) {
    // how many ? it in.
    int x = 0;
    for(int i = 0; i < p.size(); i++) {
      if(p[i] == '?') {
        x++;
      }
    }
    if(x <= s.size()) {
      return true;
    }
    return false;
  }

  int findx(const string& s, int* len) {
    int p = 0;
    while(p < s.size()) {
      if(s[p] != '?' && s[p] != '*') {
        break;
      }
      p++;
    }
    if(p == s.size()) {
      return -1;
    }
    int q = p;
    while(q < s.size() && s[q] != '?' && s[q] != '*') {
      q++;
    }
    *len = (q - p);
    return p;
  }

  bool isMatch(const string& s, const string& p) {
    if(p.empty()) {
      return s.empty();
    }
    // find split pos in p;
    int len;
    int x = findx(p, &len);
    if(x == -1) {
      return baseMatch(s, p);
    }
    string xs = p.substr(x, len);
    string pu = p.substr(0, x);
    string pv = p.substr(x + len);
    int pos = 0;
    for(;;) {
      int p = s.find(xs, pos);
      if(p == string::npos) {
        break;
      }
      // split.
      string u = s.substr(0, p);
      string v = s.substr(p + len);
      if(isMatch(u, pu) && isMatch(v, pv)) {
        return true;
      }
      pos++;
    }
    return false;
  }
};

int main() {
  Solution s;
  assert(s.isMatch("aa", "*"));
  assert(!s.isMatch("b", "*?*?"));
  assert(!s.isMatch("aa", "a"));
  printf("%s\n", s.simplify("?**?").c_str());
  printf("%s\n", s.simplify("?*?").c_str());
  printf("%s\n", s.simplify("?**").c_str());
  printf("%s\n", s.simplify("**?").c_str());
  printf("%s\n", s.simplify("*").c_str());
  printf("%s\n", s.simplify("**").c_str());
  assert(!s.isMatch("babbbbaabababaabbababaababaabbaabababbaaababbababaaaaaabbabaaaabababbabbababbbaaaababbbabbbbbbbbbbaabbb", "b**bb**a**bba*b**a*bbb**aba***babbb*aa****aabb*bbb***a"));
  return 0;
}
