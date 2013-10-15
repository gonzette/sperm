#include <string>
#include <algorithm>
#include <vector>
using namespace std;

class Solution {
public:
  string longestCommonPrefix(vector<string>& strs) {
    // Note: The Solution object is instantiated only once and is reused by each test case.
    int n = strs.size();
    if(n == 0) return "";
    int l = strs[0].size();
    for(int i = 1; i < n; i++) {
      l = min(l, (int)strs[i].size());
    }
    int res = 0;
    for(int i = 0; i < l; i++) {
      char ch = strs[0][i];
      bool ok = true;
      for(int j = 1; j < n; j++) {
        if(strs[j][i] != ch) {
          ok = false;
          break;
        }
      }
      if(ok) res++;
      else break;
    }
    return strs[0].substr(0, res);
  }
};

int main() {
  return 0;
}
