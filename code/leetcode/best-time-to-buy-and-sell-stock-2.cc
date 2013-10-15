#include <vector>
using namespace std;

class Solution {
public:
  int maxProfit(vector<int>& prices) {
    // Start typing your C/C++ solution below
    // DO NOT write int main() function
    int n = prices.size();
    if(n == 0) {
      return 0;
    }
    int x = 0;
    int result = 0;
    for(; x < n; x++) {
      if(x == 0 || (prices[x] <= prices[x - 1])) {
        continue;
      }
      // smallest.
      int s = prices[x - 1];
      for(; x < n; x++) {
        if(prices[x] <= prices[x - 1]) {
          break;
        }
      }
      // largest.
      int l = prices[x - 1];
      x--;
    }
    return result;
  }
};

int main() {
  return 0;
}
