#include <string>
#include <cassert>
using namespace std;

class Solution {
 public:
  int n;
  int m;
  char* dp;
  bool isMatch(const char *s, const char *p) {
    // Note: The Solution object is instantiated only once and is reused by each test case.
    string xs(s);
    string xp(p);
    xp = simplify(xp);
    n = xs.size();
    m = xp.size();
    if(n == 0 && m == 0) {
      return true;
    } else if(n == 0) {
      if(m == 1 && xp[0] == '*') {
        return true;
      }
      return false;
    } else if(m == 0) {
      return false;
    }
    dp = new char[n*m];
    memset(dp,0xff,n*m);
    
    bool res = isMatch(xs,0,xp,0);
    delete[] dp;
    return res;
  }

  int getIndex(int i,int j) {
    return i * m + j;
  }
  
  string simplify(const string& p) {
    char last = '\0';
    string x;
    for(int i=0;i<p.size();i++) {
      if(last == '*' && p[i] == '*') {
        continue;
      } else {
        x += p[i];
        last = p[i];
      }
    }
    return x;
  }

  
  bool isMatch(const string& s,int sf,const string& p,int pf) {
    if(sf == s.size() && pf == p.size()) {
      return true;
    } else if(sf == s.size() || pf == p.size()) {
      return false;
    }
    int index = getIndex(sf,pf);
    if(dp[index] != -1) {
      return dp[index] == 1;
    }
    bool res = false;
    if(p[pf] == '?') {
      res = isMatch(s,sf+1,p,pf+1);
    } else if(p[pf] == '*') {
      for(int i=sf;i<=s.size();i++) {
        if(isMatch(s,i,p,pf+1)) {
          res = true;
          break;
        }
      }
    } else {
      res = (p[pf] == s[sf] && isMatch(s,sf+1,p,pf+1));
    }
    dp[index] = (res ? 1 : 0);
    return res;
  }
};

int main() {
  Solution s;
  assert(s.isMatch("aa","*"));
  assert(!s.isMatch("b","*?*?"));
  printf("%s\n",s.simplify("?**?").c_str());
  printf("%s\n",s.simplify("?*?").c_str());
  printf("%s\n",s.simplify("?**").c_str());
  printf("%s\n",s.simplify("**?").c_str());
  printf("%s\n",s.simplify("*").c_str());
  printf("%s\n",s.simplify("**").c_str());
  assert(!s.isMatch("babbbbaabababaabbababaababaabbaabababbaaababbababaaaaaabbabaaaabababbabbababbbaaaababbbabbbbbbbbbbaabbb","b**bb**a**bba*b**a*bbb**aba***babbb*aa****aabb*bbb***a"));
  return 0;
}
