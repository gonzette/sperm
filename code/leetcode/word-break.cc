#include <set>
#include <map>
#include <string>
using namespace std;

#define unordered_set set

class Solution {
public:
  char* dp;
  int N;
  bool wordBreak(string s, unordered_set<string>& dict) {
    // Note: The Solution object is instantiated only once and is reused by each test case.
    const int n = s.size();
    if(n == 0) {
      return true;
    }
    N = n;
    dp = new char[n * n];
    memset(dp, 0xff, sizeof(char)*n * n);
    bool x = wb(s, 0, n - 1, dict);
    delete[] dp;
    return x;
  }
  int getIndex(int i, int j) {
    return i * N + j;
  }
  bool wb(const string& s, int f, int e, unordered_set<string>& dict) {
    if(f > e) {
      return true;
    }
    int index = getIndex(f, e);
    if(dp[index] != -1) {
      return dp[index] == 1;
    }
    string sub = s.substr(f, e - f + 1);
    bool res = false;
    if(dict.find(sub) == dict.end()) {
      for(int i = f + 1; i <= e; i++) {
        // [f,i-1],[i,e]
        bool b1 = wb(s, f, i - 1, dict);
        bool b2 = wb(s, i, e, dict);
        if(b1 && b2) {
          res = true;
          break;
        }
      }
    } else {
      res = true;
    }
    dp[index] = res ? 1 : 0;
    return res;
  }
};

int main() {
  Solution s;
  {
    set<string> dict;
    dict.insert("leet");
    dict.insert("code");
    printf("%d\n", s.wordBreak("leetcode", dict));
  }
  return 0;
}
