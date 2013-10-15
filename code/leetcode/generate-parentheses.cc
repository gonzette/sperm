#include <string>
#include <vector>
using namespace std;

class Solution {
public:
  vector<string> generateParenthesis(int n) {
    // Note: The Solution object is instantiated only once and is reused by each test case.
    vector<string> rs;
    string s;
    R(n, 0, 0, s, rs);
    //P(rs);
    //printf("%lu\n",rs.size());
    return rs;
  }
  void P(vector<string>& rs) {
    printf("[");
    for(int i = 0; i < rs.size(); i++) {
      printf("%s ", rs[i].c_str());
    }
    printf("]\n");
  }
  void R(int n, int l, int r, string& s, vector<string>& rs) {
    if(l == n && r == n) {
      rs.push_back(s);
    } else {
      // b+1 or b-1;
      if(l < n) {
        s += '(';
        R(n, l + 1, r, s, rs);
        //s.pop_back();
        s = s.substr(0, s.size() - 1);
      }
      if(r < l) {
        s += ')';
        R(n, l, r + 1, s, rs);
        //s.pop_back();
        s = s.substr(0, s.size() - 1);
      }
    }
  }
};

int main() {
  Solution s;
  s.generateParenthesis(10);
  return 0;
}
