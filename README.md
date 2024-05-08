WITH RankedUids AS (
  SELECT Uid,
         ROW_NUMBER() OVER (ORDER BY created_date ASC) AS uid_rank
  FROM your_table
  GROUP BY Uid
  FETCH FIRST 10 ROWS ONLY
),
RankedData AS (
  SELECT rd.*,
         ROW_NUMBER() OVER (PARTITION BY Uid ORDER BY created_date ASC) AS id_rank
  FROM your_table rd
  INNER JOIN RankedUids ru ON rd.Uid = ru.Uid
  WHERE ru.uid_rank <= 10
)
SELECT rd.Id
FROM RankedData rd
WHERE id_rank <= 1000;
