library(plotly)

data1 <- read.csv('C:/Users/Adrian/Downloads/relations_alone.csv',  header = FALSE, sep = ",")
data2 <- read.csv('C:/Users/Adrian/Downloads/relations_group.csv',  header = FALSE, sep = ",")
data3 <- read.csv('C:/Users/Adrian/Downloads/relations_middle.csv',  header = FALSE, sep = ",")

f <- list(family = "Courier New, monospace",size = 18,color = "#7f7f7f")

data1 <- data1[order(data1$V1),]
data2 <- data2[order(data2$V1),]
data3 <- data3[order(data3$V1),]

x <- list(
  title = "threshold",
  titlefont = f
)
y <- list(
  title = "correct classifications in %",
  titlefont = f
)
for(i in 1:11903)
{
  data1$V2[i] <- data1$V2[i]*100
  data2$V2[i] <- data2$V2[i]*100
  data3$V2[i] <- data3$V2[i]*100
}
b1 = c(13.7,97.92)
b(b) = c("x","y")

p <- plot_ly(data1, x = data1$V1, y = data1$V2, name = 'eval_alone', type = 'scatter', mode = 'lines', line = list(width = 1)) %>%
  add_trace(data2, x = data2$V1, y = data2$V2, name = 'eval_group') %>% 
  add_trace(data3, x = data3$V1, y = data3$V2, name = 'harmonic mean') %>% 
  add_trace(b1, x = b1[1], y = b1[2], name = 'best threshold', mode = 'markers', marker = list(size = 5, color = "black", symbol = 4)) %>% 
  layout(xaxis = x, yaxis = y) 
p
