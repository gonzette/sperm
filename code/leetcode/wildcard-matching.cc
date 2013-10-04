#include <string>
#include <cassert>
using namespace std;

class Solution {
 public:
  int n;
  int m;
  char* dp;
  int* valid;
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
    valid = new int[m];
    memset(valid,0,sizeof(int) * m);
    preValid(p);
    
    bool res = isMatch(xs,0,xp,0);
    delete[] dp;
    delete valid;
    return res;
  }

  int getIndex(int i,int j) {
    return i * m + j;
  }

  int preValid(const string &p) {
    valid[m-1]=0;
    for(int i=m-2;i>=0;i--) {
      if(p[i+1] == '*') {
        valid[i] = valid[i+1];      
      } else {
        valid[i] = valid[i+1] + 1;
      }
    }
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
    if(p[pf] == '*') {
      if(pf == (p.size() - 1)) { // last one. exception. matches all.
        res = true;
      } else {
        int c = valid[pf];        
        for(int i=sf;(i+c)<=s.size();i++) {
          if(isMatch(s,i,p,pf+1)) {
            res = true;
            break;
          }
        }
      }
    } else {
      res = true;
      while(pf < p.size() && sf < s.size()) {
        if(p[pf]!='*') {
          if(!(p[pf]=='?' || p[pf] == s[sf])) {
            res = false;
            break;
          }
          pf++;
          sf++;
        } else {          
          break;
        }        
      }
      if(res) {
        res = isMatch(s,sf,p,pf);
      }
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
