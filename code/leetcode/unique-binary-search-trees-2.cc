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
  vector<TreeNode*> generateTrees(int n) {
    // Note: The Solution object is instantiated only once and is reused by each test case.
    return R(1, n);
  }

  vector<TreeNode*> R(int s, int e) {
    vector<TreeNode*> res;
    if(s > e) {
      res.push_back(NULL);
    } else if(s == e) {
      res.push_back(new TreeNode(s));
    } else {
      for(int i = s; i <= e; i++) {
        vector<TreeNode*> ls = R(s, i - 1);
        vector<TreeNode*> rs = R(i + 1, e);
        for(int u = 0; u < ls.size(); u++) {
          for(int v = 0; v < rs.size(); v++) {
            TreeNode* t = new TreeNode(i);
            t->left = ls[u];
            t->right = rs[v];
            res.push_back(t);
          }
        }
      }
    }
    return res;
  }

  TreeNode* copy(TreeNode* t) {
    if(t == NULL) return NULL;
    TreeNode* x = new TreeNode(t->val);
    x->left = copy(t->left);
    x->right = copy(t->right);
    return x;
  }
};

int main() {
  return 0;
}
