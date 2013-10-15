/**
 * Definition for binary tree
 * struct TreeNode {
 *     int val;
 *     TreeNode *left;
 *     TreeNode *right;
 *     TreeNode(int x) : val(x), left(NULL), right(NULL) {}
 * };
 */

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
  vector<vector<int> > pathSum(TreeNode* root, int sum) {
    // Start typing your C/C++ solution below
    // DO NOT write int main() function
    vector<int> trace;
    vector< vector<int> > path;
    if(root) {
      R(root, trace, sum, path);
    }
    return path;
  }

  void R(TreeNode* root, vector<int>& trace, int sum, vector< vector<int> >& path) {
    if(root->left == NULL && root->right == NULL) {
      if(sum == root->val) {
        vector<int> vi(trace);
        vi.push_back(root->val);
        path.push_back(vi);
      }
    } else {
      trace.push_back(root->val);
      if(root->left) {
        R(root->left, trace, sum - root->val, path);
      }
      if(root->right) {
        R(root->right, trace, sum - root->val, path);
      }
      trace.pop_back();
    }
  }
};

int main() {
  return 0;
}
