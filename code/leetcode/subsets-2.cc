#include <vector>
#include <algorithm>
using namespace std;

class Solution {
public:
  vector<vector<int> > subsetsWithDup(vector<int>& S) {
    // Note: The Solution object is instantiated only once and is reused by each test case.
    sort(S.begin(), S.end());
    vector<int> vi;
    vector< vector<int> > res;
    R(S, 0, vi, res);
    return res;
  }
  void R(vector<int>& S, int idx, vector<int>& vi, vector< vector<int> >& res) {
    if(idx == S.size()) {
      res.push_back(vi);
      return ;
    }
    // find duplicate elements sequence.
    int e = idx;
    while(e < S.size() && S[e] == S[idx] ) e++;
    R(S, e, vi, res); // non of S[idx]
    for(int i = 0; i < (e - idx); i++) {
      vi.push_back(S[idx]); // (i+1) of S[idx]
      R(S, e, vi, res);
    }
    for(int i = 0; i < (e - idx); i++) {
      vi.pop_back();
    }
    return ;
  }
};

int main() {
  return 0;
}
