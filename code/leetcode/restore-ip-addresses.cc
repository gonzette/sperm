#include <vector>
#include <string>
using namespace std;

class Solution {
public:
  vector<string> restoreIpAddresses(string s) {
    // Note: The Solution object is instantiated only once and is reused by each test case.
    vector<string> vs, res;
    R(s, 0, vs, res);
    return res;
  }

  void R(const string& s, int idx, vector<string>& vs, vector<string>& res) {
    if(idx == s.size()) {
      if(vs.size() == 4) {
        res.push_back(vs[0] + "." + vs[1] + "." + vs[2] + "." + vs[3]);
      }
      return ;
    }
    // alreay 4 parts
    if(vs.size() == 4) return ; // prune.
    int val = 0;
    int end = s.size() - 1;
    if(s[idx] == '0') end = idx;
    for(int i = idx; i <= end; i++) {
      val = val * 10 + s[i] - '0';
      if(val > 255) break;
      vs.push_back(s.substr(idx, i - idx + 1));
      R(s, i + 1, vs, res);
      vs.pop_back();
    }
    return ;
  }
};

int main() {
  return 0;
}
