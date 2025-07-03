{-# LANGUAGE OverloadedStrings #-}
{-# LANGUAGE TemplateHaskell #-}

module Main where

import Prelude hiding (putStrLn, getContents)
import Data.Text (Text, intercalate, replicate)
import Data.Text.IO (getContents, putStrLn)
import Data.Attoparsec.Text
import Data.Sequence hiding ((|>), (<|))
import Control.Lens hiding (children, Empty)
import Control.Monad (join)
import Control.Applicative ((<|>))
import GHC.IsList (toList)

-------------- Type --------------

type Indentation = Int

data ReleaseNote = ReleaseNote
  { _note                :: Text
  , _indentation         :: Indentation
  } deriving (Show, Eq)
$(makeLenses ''ReleaseNote)

indentBy :: Indentation -> ReleaseNote -> ReleaseNote
indentBy i rn = rn & indentation +~ i

data ReleaseCategory = ReleaseCategory
  { _categoryTitle        :: Text
  , _categoryReleaseNotes :: Seq ReleaseNote
  } deriving (Show, Eq)
$(makeLenses ''ReleaseCategory)

data Release = Release
  { _categories :: Seq ReleaseCategory
  , _footer     :: Text
  } deriving (Show, Eq)
$(makeLenses ''Release)

-------------- Parser --------------

parseRelease :: Parser Release
parseRelease = do
  cats <- parseCategories
  remainder <- takeText
  return Release { _categories = cats, _footer = remainder }

parseCategories :: Parser (Seq ReleaseCategory)
parseCategories = fromList <$> many' parseCategory

parseCategory :: Parser ReleaseCategory
parseCategory = do
  _ <- skipSpace >> "##" >> skipSpace
  catTitle <- takeTill isEndOfLine
  _ <- skipSpace
  rns <- fmap (join . fromList) $ many' $ do
    rn <- parseReleaseNote
    children <- parsePullRequestBody
    _ <- skipSpace
    let indented_children = indentBy 2 <$> children
    return $ rn <| indented_children

  return ReleaseCategory { _categoryTitle = catTitle, _categoryReleaseNotes = rns }

parsePullRequestBody :: Parser (Seq ReleaseNote)
parsePullRequestBody = parsePullRequestBodyTemplate <|> parsePullRequestBodyDependabot

parsePullRequestBodyTemplate :: Parser (Seq ReleaseNote)
parsePullRequestBodyTemplate = do
  _ <- anyChar `manyTill` (skipSpace >> "## Custom Release-Notes")
  _ <- skipSpace
  fromList <$> manyTill parseReleaseNote (skipSpace >> "<!-- end-of-pr-marker -->")

parsePullRequestBodyDependabot :: Parser (Seq ReleaseNote)
parsePullRequestBodyDependabot = do
  _ <- anyChar `manyTill` (skipSpace >> "`@dependabot ignore this dependency` will close this PR and stop Dependabot creating any more for this dependency (unless you reopen the PR or upgrade to it yourself)")
  _ <- skipSpace >> "</details>" >> skipSpace
  return mempty

parseReleaseNote :: Parser ReleaseNote
parseReleaseNote = do
  indent <- Prelude.length <$> many' space
  _ <- "- "
  title <- takeTill isEndOfLine
  _ <- endOfLine
  return $ ReleaseNote { _note = title, _indentation = indent }

-------------- Formatter --------------

formatRelease :: Release -> Text
formatRelease r =
  let cats = formatCategory <$> r^.categories
      renderedCats = intercalate "\n\n\n" $ GHC.IsList.toList cats
   in renderedCats <> "\n\n" <> r^.footer

formatCategory :: ReleaseCategory -> Text
formatCategory cat =
  let notes = formatReleaseNote <$> cat^.categoryReleaseNotes
      renderedNotes = intercalate "\n" $ GHC.IsList.toList notes
   in "## " <> cat^.categoryTitle <> "\n\n" <> renderedNotes

formatReleaseNote :: ReleaseNote -> Text
formatReleaseNote rn = Data.Text.replicate (rn^.indentation) " " <> "- " <> rn^.note

-------------- Main --------------

main :: IO ()
main = do
  input <- getContents
  case parseOnly parseRelease input of
    Left _ -> putStrLn input
    Right res -> putStrLn $ formatRelease res
