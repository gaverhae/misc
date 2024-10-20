module Main (main)
where

import Prelude hiding (exp,lookup)
import qualified Control.DeepSeq
import Control.Monad (ap,liftM,void,forM,forM_,when)
import qualified Control.Monad.ST
import qualified Data.Map as Data (Map)
import qualified Data.Map
import qualified Data.Text.Lazy.Builder
import qualified Data.Vector as Data (Vector)
import qualified Data.Vector
import qualified Data.Vector.Unboxed.Mutable
import qualified Formatting
import qualified Formatting.Clock
import qualified Formatting.Formatters
import qualified System.Clock

data Op
  = Add
  | NotEq
  deriving Show

data Exp
 = Lit Int
 | Var Int
 | Set Int Exp
 | Bin Op Exp Exp
 | Do Exp Exp
 | While Exp Exp
  deriving Show

bin :: Op -> Int -> Int -> Int
bin = \case
  Add -> (+)
  NotEq -> \v1 v2 -> if v1 /= v2 then 1 else 0

ast :: Exp
ast =
  -- x = 100
  (Do (Set 0 (Lit 100))
      -- i = 1000
      (Do (Set 1 (Lit 1000))
          -- for (; i != 0;)
          (Do (While (Bin NotEq (Lit 0)
                                (Var 1))
                     -- x = (((x + 4) + x) + 3)
                     (Do (Set 0 (Bin Add (Bin Add (Bin Add (Var 0)
                                                           (Lit 4))
                                                  (Var 0))
                                         (Lit 3)))
                         -- x = ((x + 2) + 4)
                         (Do (Set 0 (Bin Add (Bin Add (Var 0)
                                                      (Lit 2))
                                             (Lit 4)))
                             -- i = i + (-1)
                             (Set 1 (Bin Add (Lit (-1))
                                             (Var 1))))))
              -- return x
              (Var 0))))

direct :: Int -> Int
direct n =
  loop 100 (n + 1000)
  where
  loop :: Int -> Int -> Int
  loop x0 i =
    if (n == i)
    then x0
    else let x1 = x0 + 4 + x0 + 3
             x2 = x1 + 2 + 4
         in loop x2 (i - 1)

newtype Env = Env (Data.Map Int Int)
  deriving Show

bottom :: Int
bottom = undefined

lookup :: Env -> Int -> Int
lookup (Env m) n = maybe bottom id (Data.Map.lookup n m)

insert :: Env -> Int -> Int -> Env
insert (Env m) n v = Env $ Data.Map.insert n v m

mt_env :: Env
mt_env = Env Data.Map.empty

reduce :: (b -> (Int, Int) -> b) -> b -> Env -> b
reduce f zero (Env m) = foldl f zero (Data.Map.toList m)

naive_ast_walk :: Exp -> Int -> Int
naive_ast_walk ex =
  \n -> let (r, _) = loop ex (insert mt_env 13 n) in r
  where
  loop :: Exp -> Env -> (Int, Env)
  loop exp0 env0 =
    case exp0 of
      Lit v -> (v, env0)
      Var n -> (lookup env0 n, env0)
      Set n exp1 -> let (v, env1) = loop exp1 env0
                    in (v, insert env1 n v)
      Bin op e1 e2 -> do
        let (v1, env1) = loop e1 env0
        let (v2, env2) = loop e2 env1
        ((bin op) v1 v2, env2)
      Do first rest -> do
        let (_, env1) = loop first env0
        loop rest env1
      While condition body -> do
        let (c, env1) = loop condition env0
        if c == 1
        then do
          let (_, env2) = loop body env1
          loop exp0 env2
        else (bottom, env1)

twe_cont :: Exp -> Int -> Int
twe_cont e =
  \n -> loop e (insert mt_env 13 n) (\_ r -> r)
  where
  loop :: Exp -> Env -> (Env -> Int -> Int) -> Int
  loop exp env cont =
    case exp of
      Lit v -> cont env v
      Var n -> cont env (lookup env n)
      Set n exp -> loop exp env (\env v -> cont (insert env n v) v)
      Bin op e1 e2 -> loop e1 env (\env v1 ->
        loop e2 env (\env v2 ->
          cont env ((bin op) v1 v2)))
      Do first rest -> loop first env (\env _ -> loop rest env cont)
      While condition body -> loop condition env (\env condition_value ->
        if (1 == condition_value)
        then loop body env (\env _ -> loop exp env cont)
        else cont env bottom)

data EvalExec a where
  EvalBind :: EvalExec a -> (a -> EvalExec b) -> EvalExec b
  EvalReturn :: a -> EvalExec a
  EvalLookup :: Int -> EvalExec Int
  EvalSet :: Int -> Int -> EvalExec ()

instance Functor EvalExec where fmap = liftM
instance Applicative EvalExec where pure = return; (<*>) = ap
instance Monad EvalExec where return = EvalReturn; (>>=) = EvalBind

twe_mon :: Exp -> Int -> Int
twe_mon exp =
  \n -> exec (eval exp) (insert mt_env 13 n) (\_ r -> r)
  where
  eval :: Exp -> EvalExec Int
  eval = \case
    Lit v -> return v
    Var n -> do
      v <- EvalLookup n
      return v
    Set n exp -> do
      v <- eval exp
      EvalSet n v
      return v
    Bin op e1 e2 -> do
      v1 <- eval e1
      v2 <- eval e2
      return $ (bin op) v1 v2
    Do first rest -> do
      _ <- eval first
      eval rest
    While condition body -> do
      c <- eval condition
      if 1 == c
      then do
        _ <- eval body
        eval (While condition body)
      else return bottom

  exec :: EvalExec a -> Env -> (Env -> a -> Int) -> Int
  exec m env cont = case m of
    EvalBind prev step -> exec prev env (\env ret -> exec (step ret) env cont)
    EvalReturn v -> cont env v
    EvalLookup n -> cont env (lookup env n)
    EvalSet n v -> cont (insert env n v) ()

compile_to_closure :: Exp -> Int -> Int
compile_to_closure e =
  let !c = compile e in \n -> (fst $ c (insert mt_env 13 n))
  where
  compile :: Exp -> Env -> (Int, Env)
  compile = \case
    Lit v -> \env -> (v, env)
    Var n -> \env -> (lookup env n, env)
    Set n exp ->
      let !f = compile exp
      in \env ->
        let (v1, env1) = f env
        in (v1, insert env1 n v1)
    Bin op e1 e2 ->
      let !f1 = compile e1
          !f2 = compile e2
      in \env ->
        let (v1, env1) = f1 env
            (v2, env2) = f2 env1
        in ((bin op) v1 v2, env2)
    Do first rest ->
      let !f = compile first
          !r = compile rest
      in \env ->
        let (_, env1) = f env
        in r env1
    While condition body ->
      let !cond = compile condition
          !bod = compile body
          !loop = \env ->
            let (c, env1) = cond env
            in if 1 == c
               then let (_, env2) = bod env1
                    in loop (env2)
               else (bottom, env1)
      in loop

data StackOp
  = StackPush Int
  | StackSet Int
  | StackGet Int
  | StackBin Op
  | StackJump Int
  | StackJumpIfZero Int
  | StackEnd
  deriving (Show)

compile_stack :: Exp -> [StackOp]
compile_stack exp =
  loop 0 exp <> [StackEnd]
  where
  loop :: Int -> Exp -> [StackOp]
  loop count = \case
    Lit v -> [StackPush v]
    Var n -> [StackGet n]
    Set n e -> loop count e <> [StackSet n]
    Bin op e1 e2 ->
      let c1 = loop count e1
          c2 = loop (count + length c1) e2
      in c1 <> c2 <> [StackBin op]
    Do first rest ->
      let c1 = loop count first
          c2 = loop (count + length c1) rest
      in c1 <> c2
    While cond body ->
      let cc = loop count cond
          cb = loop (count + length cc + 1) body
      in cc <> [StackJumpIfZero (count + length cc + 1 + length cb + 1)]
            <> cb
            <> [StackJump count]

exec_stack :: [StackOp] -> Int -> Int
exec_stack code =
  \n -> loop 0 [n]
  where
  loop :: Int -> [Int] -> Int
  loop ip stack = case code !! ip of
    StackPush v -> loop (ip + 1) (push stack v)
    StackSet n -> let (v, s) = pop stack
                  in loop (ip + 1) (set s n v)
    StackGet n -> loop (ip + 1) (push stack (get stack n))
    StackBin op -> let (a1, s1) = pop stack
                       (a2, s2) = pop s1
                   in loop (ip + 1) (push s2 ((bin op) a2 a1))
    StackJump i -> loop i stack
    StackJumpIfZero i -> let (v, s) = pop stack
                         in if v == 0
                            then loop i s
                            else loop (ip + 1) s
    StackEnd -> fst (pop stack)
  pop :: [a] -> (a, [a])
  pop ls = (head ls, tail ls)
  push :: [a] -> a -> [a]
  push ls a = a : ls
  get :: [a] -> Int -> a
  get ls pos = (reverse ls) !! pos
  set :: [a] -> Int -> a -> [a]
  set ls pos val =
    let r = reverse ls
    in reverse (take pos r ++ val : drop (pos+1) r)

exec_stack_2 :: [StackOp] -> Int -> Int
exec_stack_2 ls_code =
  \_ -> Control.Monad.ST.runST $ do
    init_stack <- Data.Vector.Unboxed.Mutable.unsafeNew 256
    go init_stack
  where
  num_vars = foldl max 0 ((flip map) ls_code (\case StackGet n -> n; StackSet n -> n; _ -> 0))
  code :: Data.Vector StackOp
  !code = Data.Vector.fromList ls_code
  go :: forall s. Data.Vector.Unboxed.Mutable.MVector s Int -> Control.Monad.ST.ST s Int
  go stack = do
    loop 0 (num_vars + 1)
    where
    loop :: Int -> Int -> Control.Monad.ST.ST s Int
    loop ip top = case (Data.Vector.!) code ip of
      StackPush v -> do
        write top v
        loop (ip + 1) (top + 1)
      StackSet n -> do
        v <- read (top - 1)
        write n v
        loop (ip + 1) (top - 1)
      StackGet n -> do
        v <- read n
        write top v
        loop (ip + 1) (top + 1)
      StackBin op -> do
        a2 <- read (top - 1)
        a1 <- read (top - 2)
        write (top - 2) (bin op a2 a1)
        loop (ip + 1) (top - 1)
      StackJump i -> loop i top
      StackJumpIfZero i -> do
        v <- read (top - 1)
        if v == 0
        then loop i (top - 1)
        else loop (ip + 1) (top - 1)
      StackEnd -> do
        v <- read (top - 1)
        return v
    write = Data.Vector.Unboxed.Mutable.write stack
    read = Data.Vector.Unboxed.Mutable.read stack

newtype Register = Register Int
 deriving Show

data RegOp
 = RegEnd Register
 | RegLoadLiteral Register Int
 | RegLoad Register Register
 | RegJumpIfZero Register Int
 | RegJump Int
 | RegBin Op Register Register Register
 | RegPlaceholder
 deriving Show

instance Functor RegExec where fmap = liftM
instance Applicative RegExec where pure = return; (<*>) = ap
instance Monad RegExec where return = RegReturn; (>>=) = RegBind
instance MonadFail RegExec where fail = error "Should not happen"

data RegExec a where
  RegBind :: RegExec a -> (a -> RegExec b) -> RegExec b
  RegReturn :: a -> RegExec a
  RegEmit :: RegOp -> RegExec ()
  RegNext :: RegExec Register
  RegPosition :: RegExec Int
  RegEmitBefore :: (Int -> RegOp) -> RegExec () -> RegExec ()
  RegHoist :: Int -> RegExec Register

data RegState = RegState { num_registers :: Int
                         , code :: [RegOp]
                         , hoisted :: Env
                         }
 deriving Show

compile_registers :: Exp -> RegState
compile_registers exp =
  exec (eval Nothing exp)
       (RegState { num_registers = (max_var exp) + 1
                 , code = []
                 , hoisted = mt_env })
       (\(Just r) s -> s { code = (code s) <> [RegEnd r]})
  where
  max_var :: Exp -> Int
  max_var = \case
    Lit _ -> 0
    Var idx -> idx
    Set idx exp1 -> max idx (max_var exp1)
    Bin _ exp1 exp2 -> max (max_var exp1) (max_var exp2)
    Do first rest ->  max (max_var first) (max_var rest)
    While cond body -> max (max_var cond) (max_var body)
  eval :: Maybe Register -> Exp -> RegExec (Maybe Register)
  eval ret = \case
    Lit v -> do
      case ret of
        Nothing -> RegHoist v >>= return . Just
        Just r -> do
          RegEmit (RegLoadLiteral r v)
          return (Just r)
    Var idx -> return $ Just $ Register idx
    Set idx exp1 -> do
      Just (Register r) <- eval (Just (Register idx)) exp1
      when (r /= idx) (RegEmit (RegLoad (Register idx) (Register r)))
      return Nothing
    Bin op exp1 exp2 -> do
      Just r1 <- eval Nothing exp1
      Just r2 <- eval Nothing exp2
      r <- case ret of
        Nothing -> RegNext
        Just r -> return r
      RegEmit (RegBin op r r1 r2)
      return $ Just r
    Do first rest -> do
      _ <- eval Nothing first
      eval Nothing rest
    While cond body -> do
      before_condition <- RegPosition
      Just condition_result <- eval Nothing cond
      RegEmitBefore (\after_body -> RegJumpIfZero condition_result after_body)
                    (do
          _ <- eval Nothing body
          RegEmit (RegJump before_condition))
      return Nothing
  exec :: RegExec a -> RegState -> (a -> RegState -> RegState) -> RegState
  exec m cur k = case m of
    RegBind ma f -> exec ma cur (\a cur -> exec (f a) cur k)
    RegReturn a -> k a cur
    RegEmit op ->
      k () (cur { code = (code cur) <> [op] })
    RegNext ->
      k (Register $ num_registers cur) cur { num_registers = (num_registers cur) + 1 }
    RegPosition ->
      k (length (code cur)) cur
    RegEmitBefore f m ->
      let nested = exec m (cur { code = (code cur) <> [RegPlaceholder]}) (\() r -> r)
          cur_len = length (code cur)
      in k () (nested { code = (code cur)
                            <> [f (length (code nested))]
                            <> (drop (cur_len + 1) (code nested)) })
    RegHoist v ->
      let r = num_registers cur
      in k (Register $ r)
           cur { num_registers = r + 1
               , hoisted = insert (hoisted cur) r v}

run_registers :: RegState -> Int -> Int
run_registers rs =
  \n -> loop 0 (insert (hoisted rs) (-1) n)
  where
  loop :: Int -> Env -> Int
  loop ip regs = case (code rs) !! ip of
    RegEnd (Register r) -> lookup regs r
    RegLoadLiteral (Register r) v -> loop (ip + 1) (insert regs r v)
    RegLoad (Register to) (Register from) -> loop (ip + 1) (insert regs to (lookup regs from))
    RegJumpIfZero (Register r) to ->
      loop (if 0 == lookup regs r then to else (ip + 1)) regs
    RegJump to -> loop to regs
    RegBin op (Register to) (Register a1) (Register a2) ->
      loop (ip + 1) (insert regs to (bin op (lookup regs a1) (lookup regs a2)))
    RegPlaceholder -> error "Invalid code"

run_registers_2 :: RegState -> Int -> Int
run_registers_2 rs = do
  let code_v :: Data.Vector RegOp
      !code_v = Data.Vector.fromList (code rs)
  let max_reg :: Int
      max_reg = num_registers rs
  let loop :: forall s. Data.Vector.Unboxed.Mutable.MVector s Int -> Int -> Control.Monad.ST.ST s Int
      loop regs ip = case (Data.Vector.!) code_v ip of
        RegEnd (Register r) -> read regs r >>= return
        RegLoadLiteral (Register to) val -> do
          write regs to val
          loop regs (ip + 1)
        RegLoad (Register to) (Register from) -> do
          v <- read regs from
          write regs to v
          loop regs (ip + 1)
        RegJumpIfZero (Register r) to -> do
          v <- read regs r
          if 0 == v
          then loop regs to
          else loop regs (ip + 1)
        RegJump to -> loop regs to
        RegBin op (Register to) (Register a1) (Register a2) -> do
          v1 <- read regs a1
          v2 <- read regs a2
          write regs to (bin op v1 v2)
          loop regs (ip + 1)
        RegPlaceholder -> error "Invalid code"
        where
        write = Data.Vector.Unboxed.Mutable.write
        read = Data.Vector.Unboxed.Mutable.read
  \_ -> Control.Monad.ST.runST $ do
    registers <- Data.Vector.Unboxed.Mutable.unsafeNew (max_reg + 1)
    forM_ (reduce (\acc el -> el:acc) [] $ hoisted rs) (\(r, v) -> do
      Data.Vector.Unboxed.Mutable.write registers r v)
    loop registers 0

bench :: Control.DeepSeq.NFData a => [Int] -> (String, Int -> a) -> IO ()
bench ns (name, f) = do
  let now = System.Clock.getTime System.Clock.Monotonic
  let raw_string = Formatting.now . Data.Text.Lazy.Builder.fromString
  let printDur = Formatting.fprint
                   (Formatting.Formatters.string
                    Formatting.%
                    raw_string " ("
                    Formatting.%
                    Formatting.Formatters.shown
                    Formatting.%
                    raw_string " runs): "
                    Formatting.%
                    Formatting.Clock.timeSpecs
                    Formatting.%
                    raw_string " ("
                    Formatting.%
                    Formatting.Formatters.string
                    Formatting.%
                    raw_string ")\n")
  let ntimes :: Int -> IO ()
      ntimes 0 = return ()
      ntimes n = Control.DeepSeq.deepseq (f n) (ntimes (n - 1))
  let per_run t1 t2 n = do
        let i1 = System.Clock.toNanoSecs t1
            i2 = System.Clock.toNanoSecs t2
            dur = ((i2 - i1) `div` n)
            (d, unit) = if dur > 10000000
                        then (dur `div` 1000000, "ms/run")
                        else if dur > 10000
                        then (dur `div` 1000, "µs/run")
                        else (dur, "ns/run")
        show d <> " " <> unit
  let run :: Int -> IO ()
      run n = do
        start <- now
        ntimes n
        end <- now
        printDur name n start end (per_run start end (fromIntegral n))
  void $ forM ns (\n -> run n)
  return ()

functions :: [(String, Int -> Int)]
functions = [
  ("direct", direct),
  ("naive_ast_walk", naive_ast_walk ast),
  ("twe_mon", twe_mon ast),
  ("compile_to_closure", compile_to_closure ast),
  ("twe_cont", twe_cont ast),
  ("exec_stack", exec_stack (compile_stack ast)),
  ("exec_stack_2", exec_stack_2 (compile_stack ast)),
  ("run_registers", run_registers (compile_registers ast)),
  ("run_registers_2", run_registers_2 (compile_registers ast))
  ]

main :: IO ()
main = do
  void $ forM functions (bench [30, 3000])

_test :: IO ()
_test = do
  print $ compile_registers ast
  print $ (map (\(_, f) -> f 0) functions)
  void $ forM functions (bench [3, 30])
  pure ()
