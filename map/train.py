import pandas as pd
import pickle
from collections import defaultdict

transactions = pd.read_csv(
    "../data/transactions.csv").sort_values(['customer_id', 't_dat'])

train, test = {}, {}
N = 5
for cust, group in transactions.groupby("customer_id"):
    items = group["article_id"].astype(int).tolist()
    if len(items) > N:
        train[cust] = items[:-N]
        test[cust] = items[-N:]

transition_counts = defaultdict(lambda: defaultdict(int))
for items in train.values():
    for i in range(len(items)-1):
        a, b = items[i], items[i+1]
        transition_counts[a][b] += 1

rows = []
for a in transition_counts:
    total = sum(transition_counts[a].values())
    scored = [(b, transition_counts[a][b]/total if total > 0 else 0)
              for b in transition_counts[a]]
    top20 = sorted(scored, key=lambda x: x[1], reverse=True)[:20]
    for b, prob in top20:
        rows.append({"source_product_id": a, "target_product_id": b,
                    "score": round(prob, 4)})

transition_df = pd.DataFrame(rows)
transition_df.to_csv("../result/item_transition_top20.csv",
                     index=False, encoding="utf-8-sig")
