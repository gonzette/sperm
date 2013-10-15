/**
 * Definition for binary tree
 * struct TreeNode {
 *     int val;
 *     TreeNode *left;
 *     TreeNode *right;
 *     TreeNode(int x) : val(x), left(NULL), right(NULL) {}
 * };
 */

#include <cstdio>
#include <vector>
using namespace std;

struct TreeNode {
  int val;
  TreeNode* left;
  TreeNode* right;
  TreeNode(int x) : val(x), left(NULL), right(NULL) {}
};

class Solution {
public:
  TreeNode* sortedArrayToBST(vector<int>& num) {
    // Note: The Solution object is instantiated only once and is reused by each test case.
    if(num.size() == 0) {
      return NULL;
    }
    return R(num, 0, num.size() - 1);
  }
  TreeNode* R(const vector<int>& num, int s, int e) {
    if(e < s) {
      return NULL;
    } else if(e == s) {
      return new TreeNode(num[s]);
    } else {
      int n = (e + s) / 2;
      TreeNode* t = new TreeNode(num[n]);
      t->left = R(num, s, n - 1);
      t->right = R(num, n + 1, e);
      return t;
    }
  }
};

int main() {
  return 0;
}
