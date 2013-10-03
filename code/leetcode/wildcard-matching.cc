#include <string>
#include <cassert>
using namespace std;

class Solution {
 public:
  bool isMatch(const char *s, const char *p) {
    // Note: The Solution object is instantiated only once and is reused by each test case.
    string xs(s);
    string xp(p);
    return isMatch(s,0,p,0);
  }

  bool isMatch(const string& s,int sf,const string& p,int pf) {
    if(sf == s.size() && pf == p.size()) {
      return true;
    } else if(sf == s.size() || pf == p.size()) {
      return false;
    }
      
    if(p[pf] == '?') {
      return isMatch(s,sf+1,p,pf+1);
    } else if(p[pf] == '*') {
      for(int i=sf;i<=s.size();i++) {
        if(isMatch(s,i,p,pf+1)) {
          return true;
        }
      }
      return false;
    } else {
      return p[pf] == s[sf] && isMatch(s,sf+1,p,pf+1);
    }
  }
};

int main() {
  Solution s;
  assert(s.isMatch("aa","*"));
  assert(!s.isMatch("b","*?*?"));
  return 0;
}
